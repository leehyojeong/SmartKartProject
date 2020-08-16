import sys
import boto3
from boto3.dynamodb.conditions import key, Attr
import flann as F
from urllib2 import Request, urlopen
import numpy as np
import cv2

ACCESS_KEY = 'AKIASZX6TMKSIMS7V5OR'
SECRET_KEY = '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'

s3 = boto3.client('s3')
all_objects = s3.list_objects(Bucket = 'fruit-bueckt-626')
file_path = './frames/'

dynamodb = boto3.resource('dynamodb')

def getProductData(table_name, category_id):
	global dynamodb
	
	table = dynamodb.Table(table_name)
	response = table.query(
			KeyConditionExpression = Key('category_id').eq(category_id)
			)
	return response['name']

if __name__ == '__main__':
	data = getProductData('product','6000095799')

