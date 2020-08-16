from urllib2 import Request, urlopen
import numpy as np
import cv2
import sys

reload(sys)
sys.setdefaultencoding('utf-8')

def getImage(img_url):
    img = urlopen('http:'+img_url).read()
    img = np.asarray(bytearray(img),dtype='uint8')
    img = cv2.imdecode(img,cv2.IMREAD_COLOR)
    try:
        img = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
    except Exception:
        print("ERROR IN CVTCOLOR COLOR_BGR2RGB")
    return img

def getSimilarity(data,target):
    maxValue = -100
    max_name = None
    for item in data:
		#print(item['name'])
        count = 0
        compare = getImage(item['img'])

        sift = cv2.xfeatures2d.SIFT_create()
        kp1,des1 = sift.detectAndCompute(target,None)
		#print(kp1)
        kp2,des2 = sift.detectAndCompute(compare,None)
        FLANN_INDEX_KDTREE = 0
        index_params = dict(algorithm = FLANN_INDEX_KDTREE,trees=5)
        search_params = dict(checks=50)
        flann = cv2.FlannBasedMatcher(index_params,search_params)
        matches = flann.knnMatch(des1,des2,k=2)
        for i,(m,n) in enumerate(matches):
            if m.distance < 0.5*n.distance:
                count+=1
        if maxValue <= count and count != 0:
            maxValue = count
            max_name = item['name']
    return maxValue,max_name


def startSimilarity(data,origin):
    result_max,result_name = getSimilarity(data,origin)
	#print("result:",result_max,result_name)
    return result_name
