# -*- coding: utf-8 -*-
"""
Created on Mon Feb 24 16:13:24 2020

@author: sejeong
"""
##
#import boto3
##
#dynamodb = boto3.resource('dynamodb',region_name='ap-northeast-2')
##
###create table
##table = dynamodb.create_table(
##        TableName='product',
##        KeySchema=[
##                {
##                        'AttributeName':'name',
##                        'KeyType':'HASH'
##                },
##                {
##                        'AttributeName':'category_id',
##                        'KeyType':'RANGE'
##                }
##        ],
##        AttributeDefinitions=[
##                {
##                        'AttributeName':'name',
##                        'AttributeType':'S'
##                },
##                {
##                        'AttributeName':'category_id',
##                        'AttributeType':'S'
##                }
##        ],
##        ProvisionedThroughput={
##                'ReadCapacityUnits':5,
##                'WriteCapacityUnits':5
##        }
##)
##
##
###테이블이 만들어질 때까지 기다림 
##table.meta.client.get_waiter('table_exists').wait(TableName='product')
#
##테이블을 불러옴
#table = dynamodb.Table('product')
#
##dynamodb에 저장하는 함수 
#def saveDynamo(table,name,img,price,gprice,category):
#    table.put_item(
#            Item={
#                    'name':name,
#                    'price':price,
#                    'gprice':gprice,
#                    'img':img,
#                    'category':category
#                    }
#            )
#


#이마트 url
emart_seed = 'http://emart.ssg.com/category/main.ssg?dispCtgId='



#엑셀 파일 읽기(세부 카테고리)
import pandas as pd


def read_file(file_name):
    global emart_seed
    category_data = pd.read_excel(file_name,sheet_name=2)#세번째 sheet의 내용을 읽어옴 
    for category in category_data.category_id:
        str_url = str(category).zfill(10)
        SEED_read(emart_seed,str_url)



#크롤링
from urllib2 import Request, urlopen
from bs4 import BeautifulSoup
import time 

#seed카테고리 페이지를 읽고 모든 페이지를 끝낼 때 까지
def SEED_read(seed,seed_id):
    #seed_id는 카테고리 아이디
    SEED = seed+seed_id
    print(SEED)
    while True:
        html = urlopen(SEED).read()#SEED페이지를 읽어옴
        soup = BeautifulSoup(html,'html.parser')
        PAGE = soup.find("div",class_="com_paginate notranslate").find('strong')#strong이 현재페이지
        print(PAGE)
        crawl(SEED,seed_id)
        next = PAGE.find_next('a')
        if next is None:
            #마지막페이지면
            print("마지막")
            break
        if next.text == '다음':
            #다음페이지 버튼을 눌러야하는 상황이라면
            SEED = seed+seed_id+'&page='+str(int(PAGE.text)+1)
        else:
            SEED = seed+seed_id+'&page='+next.text
        time.sleep(1)
        

#아이템을 가져옴 
def crawl(target,category_id):
    global table
    html = urlopen(target).read()
    bs = BeautifulSoup(html,"html.parser")
    category = (bs.find('a',class_='notranslate clickable').text).strip()#과자씨리얼빵같은 카테고리
#    print((category.text).strip())
    goodsList = bs.find('div',class_='tmpl_itemlist')
    goods = goodsList.find_all('li',class_='cunit_t232')#모든 li를 가져옴(물품정보들)
    for i in goods:
        category = category_id
        name = i.select('.clickable > .tx_ko')
        name = name[0].text
        price = i.select('.opt_price > .ssg_price')
        price = price[0].text
        gprice = i.select('.cunit_prw > .unit')
        print(name)
        if gprice == []:
            gprice = 0
        else:   
            gprice = gprice[0].text
        img = i.select('.thmb > a > img')[0].get('src')
        #saveDynamo(table,name,img,price,gprice,category)
        time.sleep(0.5)
        
        
if __name__ == '__main__':
    read_file('./category.xlsx')
        
