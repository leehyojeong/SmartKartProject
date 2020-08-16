# -*- coding: utf-8 -*-
"""
Created on Mon Feb 24 18:54:04 2020

@author: hyoju
"""

from __future__ import print_function 
import boto3
import json
import decimal

dynamodb = boto3.resource('dynamodb',
                          region_name='ap-northeast-2',
                          endpoint_url="http://localhost:8000")

table = dynamodb.Table('product')

with open("test_product.json") as json_file:
    products = json.load(json_file,
                       parse_float = decimal.Decimal)
    
    for product in products:
        name = product['name']
        category_id = product['category_id']
        p_info = product['info']
        
        print("Adding product:",name, category_id)
        
        table.put_item(
                Item={
                        'name':name,
                        'category_id':category_id,
                        'info':p_info,
                        }
                )