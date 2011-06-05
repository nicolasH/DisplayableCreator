## Displayable Creator

This java application was created to transform big images into something that allows the images to be viewable at full resolution on an iPhone or iPod touch.

It then allows one to share that something over the network. I am calling this something a "Displayable".

The current implementation tile the image and store the tiles in a sqlite db. The tiles are also created for the image at half the size, one quarter etc... until the shrunk image is smaller than an original iPhone screen resolution. The homepage of the Displayator app is at [www.displayator.com][disp].

For transferring the Displayable to the iDevice, the app uses the Multicast DNS standard (a.k.a mDNS, 'bonjour', formerly known as 'rendezvous') and a webserver, as implemented in respectively [jmDNS][jmdns] and [jetty][jetty].

You can run the latest version of it [from the displayator website][jnlp] as a "Java Web Start"* application. 

The supported images types are the standard GIF, PNG, BMP and most JPEGs. Not supported formats include JPEG with CMYK color profile, PDF and TIFFs.

On Windows, I do not know of a good free PDF renderer for transforming PDF into images. On Mac OS X, the Preview application can do it; it can also export CMYK JPEGs as PDFs and then those as PNG.   

I have also created another Java Application to rasterize PDFs but it only work for the simplest PDFs. You can see its [sources on github][pdf-jr-git], visit its [project homepage][pdf-jr] and even can [run it][pdf-jr-jnlp] (it's a JNLP). It is based on the [PDF-Renderer Library][pdf-lib] from java.net.

*:[Java Web Start][java] application: a Java application that can run on your desktop by clicking on a link in your web browser; without needing to install it permanently.

## Usage :

### - Displayable creation:

- Drop the image you want to transform into a Displayable onto the top area of the application's main window. The displayable creation will start immediately.

Done !

*By default the displayables are created in temporary files*. 

If you want you can save it to a permanent location on your computer. The following optional steps can be taken :
- You can choose the title of the Displayable as it will appear in the iPhone app.
- You can choose the name and location of the file that will be saved (but not the extension).
- You can add a description to the Displayable, which will be shown in the Displayator before the user decides to download the displayable on his iPhone.

### - Sharing the Displayable(s).

*By default, the Displayable creator start with the sharing enabled, but no Displayable loaded.*

Your iPhone/iPod touch needs to be on the same network as the computer on which you run the Displayable creator for sharing to work. Your computer have to allow the Displayable Creator application to accept incoming connections. 

- If sharing is not enabled, click on the "start sharing" button.  You might have to adjust the port to one that is not used or blocked by your firewall.
- On your iPhone/ iPod launch the Displayator app and then tap on the "Download Displayables" list item. The available images should appear shortly. You can then select one and download it.

[java]:http://www.java.com
[jnlp]:http://www.displayator.com/DisplayableCreator/DisplayableCreator.jnlp
[disp]:http://www.displayator.com
[jmdns]:http://jmdns.sourceforge.net/
[jetty]:http://eclipse.org/jetty/
[pdf-lib]:http://java.net/projects/pdf-renderer/
[pdf-jr]:http://www.niconomicon.net/projects/java/pdf-jrasterizer/
[pdf-jr-git]:https://github.com/nicolasH/pdf-jrasterizer
[pdf-jr-jnlp]:http://www.niconomicon.net/tests/maven/net/niconomicon/pdf-jrasterizer/pdf-jrasterizer.jnlp
