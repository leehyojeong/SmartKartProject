# -*- coding: utf-8 -*-
"""
Created on Fri Jun  5 22:27:20 2020

@author: sejeong
"""

import boto3
from boto3.dynamodb.conditions import Key,Attr
import flann as F
#from urllib.request import Request, urlopen
from urllib2 import Request, urlopen
import numpy as np
import cv2

ACCESS_KEY = 'AKIASZX6TMKSIMS7V5OR'
SECRET_KEY = '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'

s3 = boto3.client('s3')
all_objects = s3.list_objects(Bucket='fruit-bucket-626')
#print(all_objects['Contents'])
#for files in all_objects['Contents']:
#	print(files['Key'])
filepath = './frames/'

dynamodb = boto3.resource('dynamodb')

def getProductData(name):
    global dynamodb
    
    table = dynamodb.Table(name)
    return table.scan()

if __name__ == '__main__':
    data = getProductData('product')
	# print(len(data['Items']))
    
	#while True:
    for files in all_objects['Contents']:
		#print(files['Key'])
        filename = files['Key']
        filepath_in = filepath+filename
        s3.download_file('fruit-bucket-626',filename,filepath_in)
        target = cv2.imread(filepath_in)
        #print(target)
        target = cv2.cvtColor(target,cv2.COLOR_BGR2RGB)

        result = F.startFLANN(data['Items'],target)
        if result!=None:
            print(result)
        break
