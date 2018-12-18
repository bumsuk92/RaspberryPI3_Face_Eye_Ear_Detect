# -*- coding: utf-8 -*-
#!/usr/bin/env python

from __future__ import print_function
import scipy.io.wavfile as wavfile

class Sound():
    def __init__(self):
        self.soundStack = 0

    def soundDetect(self):
        self.soundStack = 0
        fs_rate, signal = wavfile.read("test.wav")

        for i in range(len(signal)):
            if(signal[i]>=131):
                self.soundStack = 1