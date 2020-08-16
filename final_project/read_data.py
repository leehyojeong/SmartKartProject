# -*- coding: utf-8 -*-
"""
Created on Mon Feb 24 19:10:50 2020

@author: hyoju
"""

from __future__ import print_function
import boto3
import json
import decimal 
from boto3.dynamodb.conditions import Key, Attr
from botocore.exceptions import ClientError

class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1>0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)
    
dynamodb = boto3.resource("dynamodb",
                          region_name="ap-northeast-2",
                          endpoint_url="http://localhost:8000")

table = dynamodb.Table('product')

#찾을 데이터 01 (있음)
name = "새우깡"
category_id = "과자"

#찾을 데이터 02 (없음)
#name = "프링글스"
#category_id = "과자"

try:
    response = table.get_item(
            Key = {
                    'name':name,
                    'category_id':category_id
                    }
            )
except ClientError as e:
    print(e.response['Error']['Message'])
else:
    item = response['Item']
    print("GetItem succeeded:")
    print(json.dumps(item, indent=4, cls=DecimalEncoder))
