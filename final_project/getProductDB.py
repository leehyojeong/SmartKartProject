# -*- coding: utf-8 -*-
"""
Created on Fri Jun  5 22:27:20 2020

@author: sejeong
"""

import boto3
from boto3.dynamodb.conditions import Key,Attr


dynamodb = boto3.resource('dynamodb')

table = dynamodb.Table('product')
response = table.scan()
data = response['Items']
for item in data:
	print(item['name'])
