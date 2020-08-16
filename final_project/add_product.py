# -*- coding: utf-8 -*-
import boto3

dynamodb = boto3.resource('dynamodb')

table = dynamodb.Table('product')

table.put_item(
        Item = {
            'name':'새우깡',
            'category_id':'과자',
            }
        )
