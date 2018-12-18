# -*- coding: utf-8 -*-

import RPi.GPIO as GPIO
##
from Sound import Sound
from Bouncer import Bouncer
from GpioClassLibrary import *
import time
import datetime
from Lullaby import *
import subprocess

bouncer = Bouncer()
lullaby = Lullaby()
sound = Sound()
warningStack = 0
noSleepCheckStack = 0
babyCryStack = 0
jog = Jog()

GPIO.add_event_detect(jog.jogDown, GPIO.RISING, callback=jog.jogEvent)


### 1) 수면 시작 및 종료시 DB로 데이터 전송 ###
### 2) 자동 수면 유도 기능에 따라 바운서 작동 ###

class Sleep():
    ### 초기화 하는 메소드 ###
    def __init__(self):
        self.sleepCheckFlag = 1
        lullaby.isPlaying = 0

    ### 수면 체크를 통해 DB로 데이터전송, 바운서 작동하는 메소드 ###
    def sleepCheck(self, que, client):
        global warningStack, babyCryStack, noSleepCheckStack
        bouncer.bouncerStop()

        while (True):
            sound.soundDetect()
            if (jog.babyState == 1):
                faceDetect = que.get()
                eyeDetect = que.get()
                earDetect = que.get()

                subprocess.call('. ./audio.sh', shell=True)
                print('(faceDetect, eyeDetect, earDetect): {0}, {1}, {2}'.format(faceDetect, eyeDetect, earDetect))

                if (sound.soundStack == 1):
                    noSleepCheckStack = 0
                    warningStack = 0
                    babyCryStack = babyCryStack + 1
                    cryTime = babyCryStack * 5
                    print("아이의 울음 지속 시간 : {0}초".format(cryTime))

                    if (self.sleepCheckFlag == 1):
                        if (client.autoFlag == 1):

                            if (babyCryStack == 4 * client.pushCheckFlag):
                                client.sendMsg("$;0003;A0;" + str(cryTime) + ";&")
                                client.pushCheckFlag = client.pushCheckFlag + 1

                            bouncer.bouncerStart()
                            if (lullaby.isPlaying == 1):
                                if (lullaby.isContinuePlay() == 1):
                                    pass
                                else:
                                    lullaby.playMusic()
                                    lullaby.isPlaying = 1
                            else:
                                lullaby.playMusic()
                                lullaby.isPlaying = 1

                        else:
                            if (babyCryStack == 4 * client.pushCheckFlag):
                                client.sendMsg("$;0003;A1;" + str(cryTime) + ";&")
                                client.pushCheckFlag = client.pushCheckFlag + 1

                    else:
                        self.sleepCheckFlag = 1
                        client.sendMsg("$;0003;11;;&")

                        if (client.autoFlag == 1):
                            bouncer.bouncerStart()
                            if (lullaby.isPlaying == 1):
                                if (lullaby.isContinuePlay() == 1):
                                    pass
                                else:
                                    lullaby.playMusic()
                                    lullaby.isPlaying = 1
                            else:
                                lullaby.playMusic()
                                lullaby.isPlaying = 1

                else:
                    babyCryStack = 0
                    if (faceDetect == 1):
                        warningStack = 0
                        if (eyeDetect == 0):
                            noSleepCheckStack = noSleepCheckStack + 1
                            if (noSleepCheckStack >= 2):
                                if (self.sleepCheckFlag == 1):
                                    self.sleepCheckFlag = 0
                                    bouncer.bouncerStop()
                                    if (lullaby.isPlaying == 1):
                                        lullaby.stopmusic()
                                        lullaby.isPlaying = 0
                                    client.sendMsg("$;0003;10;;&")
                                else:
                                    bouncer.bouncerStop()
                                    if (lullaby.isPlaying == 1):
                                        lullaby.stopmusic()
                                        lullaby.isPlaying = 0
                                    continue
                        else:
                            noSleepCheckStack = 0
                            if (self.sleepCheckFlag == 1):
                                if (client.autoFlag == 1):
                                    bouncer.bouncerStart()
                                    if (lullaby.isPlaying == 1):
                                        if (lullaby.isContinuePlay() == 1):
                                            pass
                                        else:
                                            lullaby.playMusic()
                                            lullaby.isPlaying = 1
                                    else:
                                        lullaby.playMusic()
                                        lullaby.isPlaying = 1
                                else:
                                    bouncer.bouncerStop()
                                    if (lullaby.isPlaying == 1):
                                        lullaby.stopmusic()
                                        lullaby.isPlaying = 0
                                continue

                            else:
                                self.sleepCheckFlag = 1
                                client.sendMsg("$;0003;11;;&")
                                if (client.autoFlag == 1):
                                    bouncer.bouncerStart()
                                    if (lullaby.isPlaying == 1):
                                        if (lullaby.isContinuePlay() == 1):
                                            pass
                                        else:
                                            lullaby.playMusic()
                                            lullaby.isPlaying = 1
                                    else:
                                        lullaby.playMusic()
                                        lullaby.isPlaying = 1
                                else:
                                    bouncer.bouncerStop()
                                    if (lullaby.isPlaying == 1):
                                        lullaby.stopmusic()
                                        lullaby.isPlaying = 0

                    else:
                        if (earDetect == 1):
                            if (warningStack >= 1):
                                warningStack = 0

                            else:
                                if (client.autoFlag == 1):
                                    continue
                                else:
                                    bouncer.bouncerStop()
                                    if (lullaby.isPlaying == 1):
                                        lullaby.stopmusic()
                                        lullaby.isPlaying = 0
                        else:
                            warningStack = warningStack + 1
                            if (warningStack >= 3):
                                client.sendMsg("$;0003;40;;&")
                                continue
                            else:
                                continue

            else:
                if (self.sleepCheckFlag == 0):
                    client.sendMsg("$;0003;11;;&")
                    self.sleepCheckFlag = 1

                else:
                    bouncer.bouncerStop()

                    if (lullaby.isPlaying == 1):
                        lullaby.stopmusic()
                        lullaby.isPlaying = 0  ##############

                noFaceDetect = que.get()
                noEyeDetect = que.get()
                noEarDetect = que.get()
                time.sleep(0.001)
                continue

        GPIO.cleanup()
