### Tile Creator App

This java application was created to transform images into something that allows the images to be viewable at full resolution on an iPhone or iPod touch.

It then allows one to share that something over the network.

The current implementation tile the image and store the tiles in a sqlite db. The tiles are also created for the image at half the size, one quarter etc... until the shrunk image is smaller than the iPhone screen.


For transferring the tile sets to the iDevice, the app uses the mDNS standard (a.k.a bojour formerly known as 'rendezvous') and a webserver, as implemented in respectively [jmDNS][jmdns] and [jetty][jetty].

You can run the current latest version of it [from the displayator website as a jnlp][jnlp] (java webstart).

The supported images types are the standard GIF, PNG, BMP and most jpgs. Not supported formats include CMYK JPEG, PDF and TIFFs.

The Java app [PDF-JRasterizer][pdf-jr] can save some PDFs to the JPEG or PNG formats. Here are the [PDF-JRasterizer project on github][pdf-jr-git] & [PDF-JRasterizer jnlp][pdf-jr-jnlp].

For PDFs that don't work with [PDF-JRasterizer][pdf-jr], Mac users can use the Preview app to save PDFs as png or JPEGs. 


### Usage :

# Phase 1 : Tile set creation.

- Select the image you want to tile by clicking on the top browse button of the app. The tiling process will start immediately.
- You can choose the title of the image as it will appear in the iphone app.
- You can choose the name of the file that will be saved (by default the same name as the original file with a different extension).
- You *have* to choose the directory in which to save the files.
- When the tiling is finished *and* the save directory is selected, the finalize button should become clickable. When you click it, the newly created tile set will appear in the list of files available for sharing bellow. 

# Phase 2 : Sharing the tile sets.
Your iphone needs to be on the same network as the computer on which you run the tile set creator for sharing to work.

- Click "start sharing".  
- On your iPhone/ iPod launch the app and then tap on the "Download images" list item. The available images should appear shortly. You can then download them.

[jnlp]:http://www.displayator.com/TileCreatorApp/jnlp/tile-creator-app.jnlp
[jmdns]:http://jmdns.sourceforge.net/
[jetty]:http://eclipse.org/jetty/
[pdf-jr]:http://www.niconomicon.net/projects/java/pdf-jrasterizer/
[pdf-jr-git]:https://github.com/nicolasH/pdf-jrasterizer
[pdf-jr-jnlp]:http://www.niconomicon.net/tests/maven/net/niconomicon/pdf-jrasterizer/pdf-jrasterizer.jnlp
