# -*- coding: utf-8 -*-
from picamera.array import PiRGBArray
import RPi.GPIO as gpio
from picamera import PiCamera
import time
import cv2

class Face:
    def __init__(self):
        self.firstTime = time.time()
        self.faceDetect = 0
        self.eyeDetect = 0
        self.earDetect = 0
        self.prevTime = 0

        self.RELAY = 18
        gpio.setmode(gpio.BCM)
        gpio.setup(self.RELAY, gpio.OUT, initial=gpio.LOW)
        self.camera = PiCamera()
        self.camera.resolution = (640, 480)
        self.camera.framerate = 32
        self.rawCapture = PiRGBArray(self.camera, size=(640, 480))

        self.faceCascade = cv2.CascadeClassifier("haarcascade_frontalface_default.xml")
        self.eyesCascade = cv2.CascadeClassifier("haarcascade_eye_tree_eyeglasses.xml")
        self.leftEarsCascade = cv2.CascadeClassifier("haarcascade_mcs_leftear.xml")
        self.rightEarsCascade = cv2.CascadeClassifier("haarcascade_mcs_rightear.xml")
        time.sleep(0.1)

    def runOpenCV(self, que):
        for frame in self.camera.capture_continuous(self.rawCapture, format="bgr", use_video_port=True):
            image = frame.array
            latestTime = time.time()
            runningTime = int(latestTime - self.firstTime)

            if (runningTime % 5 == 0 and runningTime != self.prevTime):
                self.faceDetect = 0
                self.eyeDetect = 0
                self.earDetect = 0
                self.prevTime = runningTime

                gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
                faces = self.faceCascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(50, 50))

                if type(faces) == tuple:
                    leftEar = self.leftEarsCascade.detectMultiScale(gray, 1.3, 5)
                    rightEar = self.rightEarsCascade.detectMultiScale(gray, 1.3, 5)

                    for (x, y, w, h) in leftEar:
                        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0), 3)

                    for (x, y, w, h) in rightEar:
                        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0), 3)

                    if len(leftEar) >= 1 or len(rightEar) >=1:
                        self.earDetect = 1
                    else:
                        self.earDetect = 0

                    if len(leftEar) >= 2 and len(rightEar) >= 3:
                        gpio.output(self.RELAY, False)
                    else:
                        gpio.output(self.RELAY, True)


                else:
                    for (x, y, w, h) in faces:
                        cv2.rectangle(image, (x, y), (x + w, y + h), (0, 0, 255), 2)
                        roi_gray = gray[y:y + h, x:x + w]
                        roi_color = image[y:y + h, x:x + w]
                        eyes = self.eyesCascade.detectMultiScale(roi_gray)

                        for (ex, ey, ew, eh) in eyes:
                            cv2.rectangle(roi_color, (ex, ey), (ex + ew, ey + eh), (100, 255, 255), 2)

                        if len(faces) >= 1:
                            self.faceDetect = 1
                        else:
                            self.faceDetect = 0

                        if len(eyes) >= 2:
                            self.eyeDetect = 1
                        else:
                            self.eyeDetect = 0

                        if len(faces) >= 1 and len(eyes) >= 2:
                            gpio.output(self.RELAY, False)
                        else:
                            gpio.output(self.RELAY, True)

                que.put(self.faceDetect)
                que.put(self.eyeDetect)
                que.put(self.earDetect)

            cv2.imshow("Frame", image)
            key = cv2.waitKey(1) & 0xFF
            self.rawCapture.truncate(0)

            if key == ord("q"):
                break

        gpio.output(self.RELAY, False)
        gpio.cleanup()
        self.camera.close()
        cv2.destroyAllWindows()
