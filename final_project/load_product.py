# -*- coding: utf-8 -*-
import boto3

dynamodb = boto3.resource('dynamodb')

table = dynamodb.Table('product')

response = table.get_item(
        Key = {
            'name':'새우깡',
            'category_id':'과자',
            }
        )

item = response['Item']

print(item)
