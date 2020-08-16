# -*- coding: utf-8 -*-
"""
Created on Thu Feb 27 15:55:44 2020

@author: sejeong
"""


#이마트 매장 크롤링 
from selenium import webdriver
from bs4 import BeautifulSoup
import time

chrome_options = webdriver.ChromeOptions()
chrome_options.add_argument('--headless')
chrome_options.add_argument('--no-sandbox')
chrome_options.add_argument('--disable-dev-shm-usage')

driver = webdriver.Chrome('./chromedriver',chrome_options=chrome_options)
driver.get('https://store.emart.com/branch/list.do')

import boto3
#
dynamodb = boto3.resource('dynamodb',region_name='ap-northeast-2')
table = dynamodb.Table('mart')

def setOption():
    global driver
    #이마트, 트레이더스 등 체크박스 선택
    mart = driver.find_element_by_xpath('//*[@id="branchType"]/option[2]')
    mart.click()
    
    
def getList():
    global driver
    html = driver.page_source
    soup = BeautifulSoup(html,'html.parser')
    mart_list = (soup.find("ul",id="branchList")).find_all("li")
    for mart in range(0,len(mart_list)):
        mart_click = driver.find_element_by_xpath('//*[@id="branchList"]/li['+str(mart+1)+']/a')
        mart_click.click()
        time.sleep(0.8)#페이지 정보 변하는걸 기다려 줌 
        page = driver.page_source
        mart_soup = BeautifulSoup(page,'html.parser')
        name = (mart_soup.find('div',class_='store-header')).find('h2').text
        road_name = (mart_soup.find("dl",class_="paper-data paper-address-paired c-clearfix")).find('dd').text
        print(name+" "+road_name)
        saveDB(name,road_name);
        
def saveDB(name,road_name):
    table.put_item(
            Item={
                    'name':name,
                    'road':road_name
                    }
            )
        
if __name__ == '__main__':
    setOption()
    time.sleep(1)#페이지가 변하는걸 기다려 줌 
    getList()
