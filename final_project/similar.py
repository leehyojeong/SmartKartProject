import sys
import boto3
from  boto3.dynamodb.conditions import Key,Attr
import similar_flann as flann
import cv2
import similar_flann_thread as flannthread
reload(sys)
sys.setdefaultencoding('utf-8')

dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')
all_objects = s3.list_objects(Bucket='fruit-bucket-626')

def getDynamoDBProduct(category):
    global dynamodb
    table = dynamodb.Table('product')
    response = table.scan(FilterExpression = Attr('category_id').eq(category))
    return response['Items']

def hasItem(code,name):
    global dynamodb
    table = dynamodb.Table('Item')
    response = table.scan(FilterExpression=Key('code').eq(code)&Attr('item').contains(name))
    return response['Items']>0

def getItem(code):
    global dynamodb
    table = dynamodb.Table('Item')
    response = table.scan(FilterExpression=Key('code').eq(code),ExpressionAttributeNames={'#i':'item'})
    return response['Items']

def saveDB(code,name):
    global dynamodb
    table = dynamodb.Table("Item")
  
    response = table.update_item(Key={'code':code},UpdateExpression='set #i = :l',ExpressionAttributeValues={':l':name},ExpressionAttributeNames={'#i':'item'})
    print("Save!")
    return response

if __name__ == '__main__':
    list = []
    size = len(sys.argv)-1
    if size == 0:
        saveDB("81742",list)
    else:
        for s in range(size-1):
            data = getDynamoDBProduct(sys.argv[s+1])
            filename = sys.argv[size]
            s3.download_file('fruit-bucket-626',filename,filename)
            target = cv2.imread(filename)
            target = cv2.cvtColor(target,cv2.COLOR_BGR2RGB)
            name = flann.startSimilarity(data,target)
            list.append(name)
			#hasItem("2222",name)
			#getItem('2222')
        saveDB("81742",list)
			#print("final ",flannthread.startSimilarity(data,target))
