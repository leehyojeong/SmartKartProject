# -*- coding: utf-8 -*-
from urllib2 import Request,urlopen
import numpy as np
import cv2 
import sys
import threading

reload(sys)
sys.setdefaultencoding('utf-8')

maxValue = -100
maxName = None
target = None
lock = threading.Lock()

def getImage(img_url):
	#print(img_url)
    img = urlopen('http:'+img_url).read()
    img = np.asarray(bytearray(img),dtype='uint8')
    img = cv2.imdecode(img,cv2.IMREAD_COLOR)
    try:
        img = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
    except Exception:
        print("ERROR IN CVTCOLOR")
	#print(img)
    return img


def getSimilarity(data):
    count = 0
    global target
	#print(data['name'])
    compare = getImage(data['img'])
    sift = cv2.xfeatures2d.SIFT_create()
    print(sift.detectAndCompute(target,None))
    (kp1,des1) = sift.detectAndCompute(target,None)
    print(des1)
    (kp2,des2) = sift.detectAndCompute(compare,None)
    print("dfjksldjfsdifjlskf")
    FLANN_INDEX_KDTREE = 0
    index_prams = dict(algorithm=FLANN_INDEX_KDTREE,trees=5)
    search_params = dict(checks=50)
    flann = cv2.FlannBasedMatcher(index_params,search_params)
    matches = flann.knnMatch(des1,des2,k=2)
    print("Dfsfdss")
    for i,(m,n) in enumerate(matches):
        if m.distance < 0.5*n.distance:
            count+=1
    print("for end")
    global maxValue
    global maxName
    with lock:
        if maxValue <= count and count != 0:
            maxValue = count
            maxName = data['name']

def startSimilarity(data,origin):
    global target
    target = origin
    flannThread = []
    for i in data:
        t = threading.Thread(target=getSimilarity,args=(i,))
        flannThread.append(t)
    
    for t in flannThread:
        t.start()
    for t in flannThread:
        t.join()
    global maxName
    return maxName
