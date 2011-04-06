### Tile Creator App

This java application was created to transform images into something that allows the images to be viewable at full resolution on an iPhone or iPod touch.

It then allows one to share that something over the network. I am calling this something a "Displayable".

The current implementation tile the image and store the tiles in a sqlite db. The tiles are also created for the image at half the size, one quarter etc... until the shrunk image is smaller than the iPhone screen.


For transferring the Displayable to the iDevice, the app uses the Multicast DNS standard (a.k.a mDNS, 'bonjour', formerly known as 'rendezvous') and a webserver, as implemented in respectively [jmDNS][jmdns] and [jetty][jetty].

You can run the latest version of it [from the displayator website][jnlp] as a "Java Web Start"* application.

The supported images types are the standard GIF, PNG, BMP and most jpgs. Not supported formats include CMYK JPEG, PDF and TIFFs.

On Windows, I do not know of a good free PDF renderer for transforming PDF into images. On Mac OS X, the Preview application can do it; it can also export CMYK JPEGs as PDFs and then those as PNG.   

I have also created a Java Application to rasterize PDFs but it only work for the simplest PDFs. You can see its [sources on github][pdf-jr-git], visit its [project homepage][pdf-jr] is here, and you can [run it as a JNLP][pdf-jr-jnlp]. It is based on the PDF-Renderer Library from java.net .

*:[Java Web Start][java] application: a Java application that can run on your desktop without needing to install it permanently.

### Usage :

# Phase 1 : Displayable creation.

- Drop the image you want to transform into a Displayable onto the top area of the application (by clicking on the top browse button of the app. The tiling process will start immediately.

If you want, after the Displayable is created, you can save it someplace. The following actions can be taken :
- You can choose the title of the image as it will appear in the iphone app.
- You can choose the name of the file that will be saved (by default the same name as the original file with a different extension).

# Phase 2 : Sharing the Displayable(s).
By default, the Displayable creator start with the sharing enabled, but no Displayable loaded.

Your iPhone/iPod touch needs to be on the same network as the computer on which you run the Displayable creator for sharing to work.

- Sharing is not enabled, click "start sharing".  
- On your iPhone/ iPod launch the Displayator app and then tap on the "Download images" list item. The available images should appear shortly. You can then select one and download it.

[java]:http://www.java.com
[jnlp]:http://www.displayator.com/DisplayableCreator/DisplayableCreator.jnlp
[jmdns]:http://jmdns.sourceforge.net/
[jetty]:http://eclipse.org/jetty/
[pdf-jr]:http://www.niconomicon.net/projects/java/pdf-jrasterizer/
[pdf-jr-git]:https://github.com/nicolasH/pdf-jrasterizer
[pdf-jr-jnlp]:http://www.niconomicon.net/tests/maven/net/niconomicon/pdf-jrasterizer/pdf-jrasterizer.jnlp
