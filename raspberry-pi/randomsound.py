"""
Provides RandomSound, a simple way to play a random sound file from a
directory using pygame.
"""

from os.path import join
from glob import glob
from random import choice
from time import sleep

from pygame import mixer

class RandomSound:
    """ A simple way to play random sound files """
    def __init__(self):
        mixer.init()
        self.playing = None

    def play_from(self, path):
        """ Play a random .wav file from path if none is currently playing """
        if mixer.music.get_busy() and self.playing == path:
            return

        filename = choice(glob(join(path, '*.wav')))
        mixer.music.load(filename)
        mixer.music.play()
        self.playing = path

    def wait(self):
        """ Wait for the current sound to finish """
        while mixer.music.get_busy():
            sleep(0.1)

    def stop(self):
        """ Stop any playing sound """
        mixer.music.stop()


def main():
    """ Play a single sound and wait for it to finish """
    from sys import argv
    rs = RandomSound()
    rs.play_from(argv[1])
    rs.wait()


if __name__ == '__main__':
    main()
