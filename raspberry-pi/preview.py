"""
Show a camera preview using the same resolution and FPS settings
as will be used for training, but without randomly adjusting
the exposure (training_mode=False). Enable this to see what it does.
"""

from sys import stderr
from time import time, sleep

import pygame
from camera import Camera
import socket
import json
import textwrap

def serverinfortopreview(csocket, command):
    main(csocket, command)

def main(clientsocket, servercommand):
    camera = Camera(training_mode=False)

    while True:
        delay = 1
        started = time()
        while time() - started < delay:
            sleep(0.1)

        frame = camera.next_frame()
        package = json.dumps(frame.tolist())
        clientsocket.send(servercommand + ": %d length frame sending...\n" % len(frame.tolist()))

if __name__ == '__main__':
    main()
