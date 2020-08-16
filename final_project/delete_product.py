# -*- coding: utf-8 -*-
import boto3

dynamodb = boto3.resource('dynamodb')

table = dynamodb.Table('product')

table.delete_item(
        Key = {
            'name':'test_chips',
            'category_id':'test_chips_category',
            }
        )
