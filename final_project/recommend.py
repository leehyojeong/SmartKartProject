# -*- coding: utf-8 -*-
"""
Created on Sun Jun  7 12:37:06 2020

@author: sejeong
"""

import boto3
from boto3.dynamodb.conditions import Key,Attr
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

dynamodb = boto3.resource('dynamodb')

def getProductData(name):
    global dynamodb
    
    table = dynamodb.Table(name)
    return table.scan()


def setRecommendData(dbname,name,recommend):
    global dynamodb
    table = dynamodb.Table(dbname)
    table.put_item(Item={'name':name,'recommend':recommend})

def makeTable(data):
    code_array = []
    name_array = []
    buy_array = []
    for index in range(len(data)):
        code = data[index]["code"]
        for value in range(len(data[index]["item"])):
            code_array.append(code)
            name_array.append(data[index]["item"][value])
            buy_array.append(1)
    table_dict = {"code":code_array,"name":name_array,"buy":buy_array}
    table = pd.DataFrame(table_dict)
    return table

def getRecommend(target):
    data = getProductData("Item")
    data = data["Items"]
    table = makeTable(data)
    user_item_table = table.pivot_table('buy',index='name',columns='code')
    user_item_table.fillna(0,inplace=True)
    item_based_collabor  = cosine_similarity(user_item_table)
    item_based_collabor = pd.DataFrame(data=item_based_collabor,index=user_item_table.index,columns=user_item_table.index)
    item_based_collabor.head()
    recommend = item_based_collabor[target].sort_values(ascending=False).index.tolist()
    setRecommendData('recommend',target,recommend)

if __name__ == '__main__':
    data = getProductData('Item')
    print(data['Items'][0]['item'])
    for user in range(len(data['Items'])):
        if len(data['Items'][user]['item'])>0:
            for item in data['Items'][user]['item']:
                getRecommend(item)
	#getRecommend("멕시코산 썬팜 스낵팩 바나나 700g내외 (봉)")
