import socket
from subprocess import call
from subprocess import Popen
from _thread import *
import multiprocessing
from record import serverinfotorecord
from train import serverinfototrain
from run import serverinfotorun
from preview import serverinfotopreview

ipaddress = (([ip for ip in socket.gethostbyname_ex(socket.gethostname())[2] if not ip.startswith("127.")] or [[(s.connect(("8.8.8.8", 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]]) + ["no IP found"])[0]
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((ipaddress, 12345))
print(ipaddress)
s.listen(5)

print('Waiting for a connection.')
def threaded_client(clientsocket):
    ps = []
    while True:
        data = (clientsocket.recv(8)[2:]).decode("utf-8")
        if data == "CON":
            print(data)
            clientsocket.send(data + 'CON-ACK: connected to '+ipaddress+'\n')
        elif not data:
        	break
        elif data == "REQ" or data == "RNE":
            print(data)
            p = multiprocessing.Process(target=serverinfotorecord, args=(clientsocket, data,))
            ps.append(p)
            p.start()
        elif data == "TRN":
        	print(data)
            p = multiprocessing.Process(target=serverinfototrain, args=(clientsocket, data,))
            ps.append(p)
            p.start()
        elif data == "RUN":
            print(data)
            p = multiprocessing.Process(target=serverinfotorun, args=(clientsocket, data,))
            ps.append(p)
            p.start()
        elif data == "CLR":
            print(data)
            clientsocket.send(data + '\n')
        elif data == "RMV":
        	ps.append(Popen(['rm', 'records/rne']))
        	ps.append(Popen(['rm', 'records/req']))
        	ps.append(Popen(['rm', 'records/model.h5']))
        	clientsocket.send(data + '-ACK: All training data deleted\n')
        elif data == "CAM":
            print(data)
            p = multiprocessing.Process(target=serverinfotopreview, args=(clientsocket, data,))
            ps.append(p)
            p.start()
        elif data == "STP" or data == "DCN":
            if len(ps) != 0:
                procount = len(ps)
                for p in ps:
                    p.terminate()
            print(data)
            clientsocket.send(data + '-ACK: ' + str(procount) + ' process killed\n')
    clientsocket.close()

while True:

    clientsocket, addr = s.accept()
    print('connected to: '+addr[0]+':'+str(addr[1]))

    start_new_thread(threaded_client,(clientsocket, ipaddress,))