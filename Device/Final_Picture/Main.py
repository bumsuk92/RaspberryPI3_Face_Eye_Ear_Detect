# -*- coding: utf-8 -*-

import threading
import Queue
from SleepPattern import Sleep
from FaceDetect import *
from ConnectServer import *

if __name__ == "__main__":
    import sys, getopt

    args, video_src = getopt.getopt(sys.argv[1:], '', ['cascade=', 'nested-cascade='])

    try:

        video_src = video_src[0]

    except:

        video_src = 0

    args = dict(args)

    que = Queue.Queue()
    # networkQue = Queue.Queue()
    face2 = Face(args, )
    sleep = Sleep()
    client = ConnectServer()
    client.connectToServer()

    t1 = threading.Thread(target=face2.runOpenCV, args=(que, video_src))
    t2 = threading.Thread(target=sleep.sleepCheck, args=(que, client))
    t3 = threading.Thread(target=client.receiveMsg, args=(client.sock,))
    t1.start()
    t2.start()
    t3.daemon = True

    t3.start()
