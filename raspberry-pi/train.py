"""
Train a classifier to distinguish between several sets of images. This
uses a pretrained feature extractor (PiNet) to convert images to features
then trains a very simple Keras classifier on those features.
"""

from sys import argv, exit, stdout
from cPickle import load
from tensorflow import keras
from pinet import PiNet
import numpy as np
import socket

def serverinfototrain(csocket, command):
    main(csocket, command)

def main(clientsocket, servercommand):
    model_file = 'records/model.h5'
    recording_files = 'records/req', 'records/rne'

    clientsocket.send(servercommand + ': Loading...\n')
    print('Loading tensorflow feature extractor...')
    feature_extractor = PiNet()

    # Load the data from the recording files and hope we don't run out of RAM
    # TensorFlow and Keras support streaming data from files if you want to use more data
    stdout.write("Loading")
    xs = []
    ys = []
    class_count = {}

    # We loop through the recording files and preprocess each frame with the NN feature extractor
    # These features are what our classifier will use as inputs (xs)
    # It will try to predict which file id they correspond to (ys)
    clientsocket.send(servercommand + ': Loading records...\n')
    for i, filename in enumerate(recording_files):
        stdout.write(' %s' % filename)
        stdout.flush()
        with open(filename, 'rb') as f:
            x = load(f)
            features = [feature_extractor.features(f) for f in x]
            label = np.zeros((len(recording_files),))
            label[i] = 1.          # Make a label with a 1 in the column for the file for this frame
            xs += features         # Add the features for the frames loaded from this file
            ys += [label] * len(x) # Add a label for each frame in the file
            class_count[i] = len(x)
    stdout.write('\n')

    clientsocket.send(servercommand + ': Training...\n')
    print("Creating a network to classify %s" % ', '.join(recording_files))
    classifier = make_classifier(xs[0].shape, len(recording_files))

    print("Training the network to map high-level features to %d categories" % len(recording_files))
    classifier.fit([np.array(xs)], [np.array(ys)], epochs=20, shuffle=True)

    clientsocket.send(servercommand + ': Saving the model...\n')
    print("Now we save this model so we can deploy it whenever we want")
    classifier.save(model_file)

    clientsocket.send(servercommand + '-ACK: Model saved\n')
    print("All done, model saved in %s" % model_file)


def make_classifier(input_shape, num_classes):
    """ Make a very simple classifier
        Layers:
            GaussianNoise: Add random noise to prevent our classifier memorizing specific examples.
            Flatten: The input may come from a layer with shape (x, y, depth); flatten it to 1D.
            Dense: Provide one output per class scaled to sum to 1 (softmax) """

    # Define a simple neural network
    net_input = keras.layers.Input(input_shape)

    noise = keras.layers.GaussianNoise(0.3)(net_input)
    flat = keras.layers.Flatten()(noise)
    net_output = keras.layers.Dense(num_classes, activation='softmax')(flat)

    net = keras.models.Model([net_input], [net_output])

    # Compile a model before use. The loss should match the output activation function, e.g.
    # binary_crossentropy for sigmoid, categorical_crossentropy for softmax, mse for linear.
    # Adam is a solid default optimizer, we can leave the learning rate at the default.
    net.compile(optimizer=keras.optimizers.Adam(),
                loss='categorical_crossentropy',
                metrics=['accuracy'])
    return net


if __name__ == '__main__':
    main()
