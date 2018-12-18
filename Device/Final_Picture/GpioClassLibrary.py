# -*- coding: utf-8 -*-

import RPi.GPIO as GPIO

class Jog():
    def __init__(self):

        GPIO.setmode(GPIO.BCM)

        self.jogDown = 6
        GPIO.setup(self.jogDown, GPIO.IN)

        self.babyState = 0
        self.bouncerWeight = 5
        self.babyWeight = 3

        self.jogState = 1

    def jogEvent(self, channel):
        if channel == self.jogDown:
            if (self.jogState == 1):
                self.bouncerWeight = self.bouncerWeight + self.babyWeight
                print("아기 (O)  현재 무게: {0}kg".format(self.bouncerWeight))
                self.babyState = 1
                self.jogState = 0

            else:
                self.bouncerWeight = self.bouncerWeight - self.babyWeight
                print("아기 (X)  현재 무게: {0}kg".format(self.bouncerWeight))
                self.babyState = 0
                self.jogState = 1

class DcMotor():
    ports = {}

    def __init__(self, pins, direction):

        for i in range(len(pins)):
            self.ports[i] = pins[i]

        GPIO.setwarnings(False)
        GPIO.setmode(GPIO.BCM)

        i = 0
        for port in pins:
            if (direction[i] == 'in'):
                GPIO.setup(port, GPIO.IN)
            else:
                GPIO.setup(port, GPIO.OUT)
            i += 1

    def dcMotorCw(self):
        GPIO.output(self.ports[0], True)
        GPIO.output(self.ports[1], False)
        GPIO.output(self.ports[2], True)

    def dcMotorCcw(self):
        GPIO.output(self.ports[0], False)
        GPIO.output(self.ports[1], True)
        GPIO.output(self.ports[2], True)

    def dcMotorStop(self):
        GPIO.output(self.ports[0], False)
        GPIO.output(self.ports[1], False)
        GPIO.output(self.ports[2], True)

    def dcMotorShortBreak(self):
        GPIO.output(self.ports[2], False)
        

        
    