# -*- coding: utf-8 -*-

import threading
import Queue
from SleepPattern import Sleep
from FaceDetect import *
from ConnectServer import *

if __name__ == "__main__":
    que = Queue.Queue()
    face2 = Face()
    sleep = Sleep()
    client = ConnectServer()
    client.connectToServer()

    t1 = threading.Thread(target=face2.runOpenCV, args=(que,))
    t2 = threading.Thread(target=sleep.sleepCheck, args=(que, client))
    t3 = threading.Thread(target=client.receiveMsg, args=(client.sock,))
    t1.start()
    t2.start()
    t3.daemon = True

    t3.start()
