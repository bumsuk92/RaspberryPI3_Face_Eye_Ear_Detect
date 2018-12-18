import pygame

class Lullaby():
    def __init__(self):
        self.isPlaying = 0

    def pmusic(self, file):
        pygame.init()
        pygame.mixer.init()
        pygame.mixer.music.load(file)
        pygame.mixer.music.play()


    def stopmusic(self):
        pygame.mixer.music.stop()

    def getmixerargs(self):
        pygame.mixer.init()
        freq, size, chan = pygame.mixer.get_init()
        return freq, size, chan

    def initMixer(self):
        BUFFER = 3072
        FREQ, SIZE, CHAN = self.getmixerargs()
        pygame.mixer.init(FREQ, SIZE, CHAN, BUFFER)

    def playMusic(self):
        self.initMixer()
        file = './lullaby.mp3'
        self.pmusic(file)

    def isContinuePlay(self):
        if (pygame.mixer.music.get_busy() == 1):
            return 1
        else:
            return 0
