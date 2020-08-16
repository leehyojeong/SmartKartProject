# -*- coding: utf-8 -*-
"""
Created on Sat Feb 29 16:59:56 2020

@author: sejeong
"""

from selenium import webdriver
from bs4 import BeautifulSoup
import time

chrome_options = webdriver.ChromeOptions()
chrome_options.add_argument('--headless')
chrome_options.add_argument('--no-sandbox')
chrome_options.add_argument('--disable-dev-shm-usage')

driver = webdriver.Chrome('./chromedriver',chrome_options = chrome_options)
driver.get('http://emart.ssg.com/planshop/main.ssg?Egnb=planshop')

import boto3
#
dynamodb = boto3.resource('dynamodb',region_name='ap-northeast-2')
#
##create table
#table = dynamodb.create_table(
#        TableName='event',
#        KeySchema=[
#                {
#                        'AttributeName':'name',
#                        'KeyType':'HASH'
#                },
#                {
#                        'AttributeName':'img',
#                        'KeyType':'RANGE'
#                }
#        ],
#        AttributeDefinitions=[
#                {
#                        'AttributeName':'name',
#                        'AttributeType':'S'
#                },
#                {
#                        'AttributeName':'category_id',
#                        'AttributeType':'S'
#                }
#        ],
#        ProvisionedThroughput={
#                'ReadCapacityUnits':5,
#                'WriteCapacityUnits':5
#        }
#)
#
#
##테이블이 만들어질 때까지 기다림 
#table.meta.client.get_waiter('table_exists').wait(TableName='product')

#테이블을 불러옴
table = dynamodb.Table('event')

def getEvent():
    global driver
    html = driver.page_source
    soup = BeautifulSoup(html,'html.parser')
    event_list = (soup.find("ul",class_="cmplan_gridlist cmplan_gridlist_w300")).find_all("li",class_="cmplan_griditem")
    for index in range(0,len(event_list)):
        img = event_list[index].find("img",class_="cmplan_img").get("src")
        title = event_list[index].find("em",class_="cmplan_tit").text
        cate = event_list[index].find("p",class_="cmplan_tit2").text
        print(img,title,cate)
        event_list_click = driver.find_element_by_xpath('//*[@id="content"]/div[3]/div/ul/li['+str(index+1)+']')
        event_list_click.click()
        time.sleep(1)
        list_html = driver.page_source
        getList(list_html)
#        getList(driver)
      
    


def getList(page):
    print("GET LIST")
    soup = BeautifulSoup(page,'html.parser')
    item_list = soup.find_all("div",class_="cunit_prod")
    print(len(item_list))
    for item in range(0,len(item_list)):
        img = item_list[item].find("img").get("src")
        name = item_list[item].find("img").get("alt")
        print(img,name)
        saveDB(name,img)
        #가격정보도 가져오기 ? 
        
def saveDB(name,img):
    table.put_item(
            Item={
                    'name':name,
                    'img':img
                    }
            )
            
if __name__ == "__main__":
    getEvent()
