# -*- coding: utf-8 -*-
"""
Created on Fri Jun  5 23:36:11 2020

@author: sejeong
"""

from urllib2 import Request, urlopen
#from urllib.request import Request, urlopen
import concurrent.futures
import numpy as np
import cv2
import sys
import matplotlib.pyplot as plt
#from importlib import reload
import boto3
from pprint import pprint

reload(sys)
sys.setdefaultencoding('utf-8')

error_cnt=0

def returnImage(img_url):
	# print(img_url)
    img = urlopen("http:"+img_url).read()
    img = np.asarray(bytearray(img),dtype="uint8")
    img = cv2.imdecode(img,cv2.IMREAD_COLOR)
	# print(img)
    try:
        img = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
		# print(img)
    except Exception:
        print("Error to cvtColor")
	#cv2.imshow('frame',img)
    return img

def compareProduct(data,origin):
    maxV = -100
    max_name = None
    for item in data:
        count = 0
        compare = returnImage(item['img'])
        
        sift = cv2.xfeatures2d.SIFT_create()
        kp1,des1 = sift.detectAndCompute(origin,None)
        kp2,des2 = sift.detectAndCompute(compare,None)
        
        FLANN_INDEX_KDTREE = 0
        
        index_params = dict(algorithm = FLANN_INDEX_KDTREE,trees = 5)
        search_params = dict(checks=50)
        
        flann = cv2.FlannBasedMatcher(index_params,search_params)
        
        matches = flann.knnMatch(des1,des2,k=2)
        
#       matchesMask = [[0,0] for i in range(len(matches))]
        
        for i,(m,n) in enumerate(matches):
            if m.distance < 0.5*n.distance:
                count+=1
        if maxV <= count and count != 0:
			#print("**"+item['name'])
            maxV = count
            max_name = item['name']
            print("###",max_name)
        print(item['name'])
    #global error_cnt
    #print("error"+error_cnt)
	#error_cnt=error_cnt+1
    return maxV,max_name

#item 테이블에 유사도 확인된 아이템 이름 넣는 함수
def put_itemlist(code,item_list,dynamodb=None):
    if not dynamodb:
        dynamodb = boto3.resource('dynamodb')
    table = dynamodb.Table('Item')
    response = table.put_item(
            Item = {
                'code':code,
                'item':item_list})
    return response

def startFLANN(data,origin):
    result_max = 0
    result_name = None
#    size = int(len(data)/100)
    size = 1
    future = np.full(size,None)
    pool = concurrent.futures.ThreadPoolExecutor(max_workers=size)
	#sndItemList = []
    
    start = 0
    end = 100
#	for i in range(size):
#        future[i] = pool.submit(compareProduct,(data[start:end]),origin)
#        start = end+1
#        end += 60
    future = pool.submit(compareProduct,(data[start:end]),origin)    
    check = np.full(size,False)
    
    while(True):
        for i in range(size):
			#            if future[i].done():
            if future.done():    
                check[i] = True
            else:
                check[i] = False
        if len(np.where(check)[0])==size:
            sndItemList = []
            print("&&&&&&&&&&&&&&&")
            for j in range(size):
                if result_max < future[j].result()[0]:
                     print("final "+future[j].result()[1])
                     result_max = future[j].result()[0]
                     result_name = future[j].result()[1]
#                 if result_max < future.result()[0]:
#                      print("final "+future.result()[1])
#                      result_max = future.result()[0]
#                      result_name = future.result()[1]
				# sndItemList.append(result_name)					
				# put_itemlist('00000',sndItemList)
			#break
	return result_name
 
