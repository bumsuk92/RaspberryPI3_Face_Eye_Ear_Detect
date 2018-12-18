# -*- coding: utf-8 -*-

from GpioClassLibrary import DcMotor

motorPins = (19, 13, 26)
directions = ('out', 'out', 'out')

dcMotor = DcMotor(motorPins, directions)


class Bouncer():

    def bouncerStart(self):
        dcMotor.dcMotorCw()

    def bouncerStop(self):
        dcMotor.dcMotorStop()

