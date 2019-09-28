# A content-based image search app

## Introduction

The main function of our app is content-based image search. After importing your local images to database, you could search your image either by the **object**
(**object detection**) or by the **text** (**optical character recognition**) or both. For example, if there is an **apple** in a photo you took long time ago, when you type **apple** in the search bar, this photo
will appear. If a photo contains a text of **your name**, when you search your name, this photo will also appear. 

Currently our app only supports English character recognition and object detection can recognize 121 different classes. The full list of supported classes can be 
found [here](app/src/main/assets/labelmap.txt). The accuracy of each classes varies a lot. The most accurate classes we know so far are: Apple, Cat, Woman, Laptop, Computer keyboard, Chair, Table, Car, Traffic light. 

## Installation

The apk file is in folder **memory\release**. *memory.apk* is the content-based image search app while *ObjectDetect.apk* is the app for demonstrating our object detection model. 

## How to use our app
1. Analyze local images and store analysis result to remote database

On main screen, click ***+ select***, you will enter the select image screen. You can check *zero* to *many* images as you want.
You could also choose ***check all*** to select all images in this folder on the ***upper left corner***. On the ***uppder middle*** of the screen, 
you could change your current image folder. After you finish, click ***Done*** button on the ***upper right corner***, a Dialog will jump out
to show how many images you have selected. The image analysis and addition to database will be executed on another thread. So now you
will return to the main screen to explore other functions. 

2. Synchronize local image database with remote image database

On main screen, click ***synchronize***, the program will scan all the images in your local storage and then compare it to last image loading operation.
If there are new images being added or deleted in local storage, it will add or delete them in remote database. Again, this operation is running
on another thread, so you could still explore other features after clicking synchronize.

3. Set you searching options

Slide on the left side of the screen, you will see the ***navigation bar*** where you can find ***Settings***. Click on ***Settings*** you will
enter the setting screen. Here you can set your search options, either by ***labels*** ***(based on object*** ***detection***, ***text(based on OCR)*** or 
both. By default, the image search is done by both. On this screen you can also clear search history and change theme. 

4. Start searching

Return to the main screen. Click on the ***search input bar***, you can type anything you want to search. Your search history will be displayed
as well so you could choose past search input. After finishing type, press enter in your keyboard, our app will send request to search images in
database. If an image meets you input is found, our app will enter the search result screen where all the result images are shwon in a grid view. 
If no image is found, a toast will appear saying that *no image found*. In the search result screen, you can click on an image to explore its detailed 
information. So you will enter image detail screen where only one images is displayed on the entire screen. On this screen, you can share this image
to your friends by clicking ***share***. You can also click ***more*** to see the datetime, size and absolute path of this image. You can also click 
***previous*** or ***next*** to see previous or next picture. 


5. Other functions 

On the ***navigation bar***, you can click on ***Gallery*** to view all the images in your local storage. You can also click on ***Camera*** to take a picture.
After taking a picture or select an image from you gallery, you can share it with your friends. 







## Acknowledgement

We would like to express our sincere appreciation for all these repository owners listed below. We could not have finished such a 
big project without their open-source repositories. We would also like to thank all other bloggers who either post blogs or upload 
videos that help us a lot in learning Android programming. 





## Reference souce

In the object detection part, some of the classes that interpret our Object Detection model are from this github repository
    https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android/app
    
We adapted the example code of Google Optical Character Recognizer(OCR) into our own Class *OCRAnalyzer*. The source code from 
	https://github.com/komamitsu/Android-OCRSample.git
	
To make the image view zoomable, we adapted the Class *ZoomageView*. Retrieved from
	https://github.com/jsibbold/zoomage.git
	
To get the screen information of mobile phone, we adapted the class *ScreenUtils* from 
	https://github.com/YancyYe/GalleryPick.git
	
In loading the images to gridView, we use an open source library *Glide* to make the scroll operation more smooth. Retrived from 
	https://github.com/bumptech/glide.git
	
To get a unique device ID, we adapted the code from 
	https://github.com/fwh007/Lib-DeviceId.git
	
Based on given Uri, get absolute path of an image:
	http://www.jianshu.com/p/b168cbe50066



