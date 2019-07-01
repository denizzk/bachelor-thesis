"""
Record frames from the camera and save them to a file. There are many
ways to do this; the method here (pickle raw frames) is a compromise
between requiring few dependencies, being easy to understand, allowing
per-frame data augmentation and performance on a Pi Zero.
"""
from time import time, sleep
from sys import argv, exit, stdout
from cPickle import load, dump
import pickle
from subprocess import Popen
import os
from os.path import exists
from os import getenv
import pygame
from camera import Camera
import socket

def serverinfotorecord(csocket, command):
    main(csocket, command)

SHOW_UI = getenv("DISPLAY")

if SHOW_UI:
    pygame.init()

def main(clientsocket, servercommand):
    clientsocket.send(servercommand + ': Recording...\n')
    name = 'records/' + servercommand.lower()
    seconds = 5

    # Initialize the camera and record video frames for a few seconds
    # We select a random exposure for each frame to generate a wide range
    # of lighting samples for training
    camera = Camera(training_mode=True)
    record(clientsocket, servercommand, camera, name, seconds)


def status(text):
    """ Show a status update to the command-line and optionally the UI if enabled """
    if SHOW_UI:
        pygame.display.set_caption(text)
    stdout.write('\r%s' % text)
    stdout.flush()


def record(camera, filename, seconds):
    """ Record from the camera """

    delay = 3 # Give people a 3 second warning to get ready
    started = time()
    while time() - started < delay:
        sleep(0.1)

    frames = []
    started = time()
    while time() - started < seconds:
        frame = camera.next_frame()
        frames.append(frame)

        # Update our progress
        status("Recording [ %d frames, %3.0fs left ]" %
               (len(frames), max(0, seconds - (time() - started))))

        # Show the image in a preview window so you can tell if you are in frame
        if SHOW_UI:
            surface = pygame.surfarray.make_surface(frame)
            screen.blit(pygame.transform.scale(surface, (512, 512)), (0, 0))
            pygame.display.flip()
            for evt in pygame.event.get():
                if evt.type == pygame.QUIT:
                    pygame.quit()
                    exit(1)

    print('')

    # Save the frames to a file, appending if one already exists
    Popen(['sudo', 'chmod', '777', filename])
    if exists(filename):
        clientsocket.send('%s: Already exist dataset found, merging datasets...\n' % servercommand)
        print("%s already exists, merging datasets" % filename)
        existing = load(open(filename, 'rb'))
        frames += existing

    clientsocket.send('%s: Writing %d frames...\n' % (servercommand, len(frames))
    stdout.write('Writing %d frames to %s... ' % (len(frames), filename))
    stdout.flush()
    dump(frames, open(filename, 'wb'))
    print('done.')
    clientsocket.send('%s-ACK: %d frames\n' % (servercommand, len(frames))
