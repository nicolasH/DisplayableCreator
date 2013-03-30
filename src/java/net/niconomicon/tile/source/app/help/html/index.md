# Displayable Creator Help



## Principle

The aim of the Displayable creator is to create Displayable and to
allow you to open and view Displayables. A Displayable is created from
a usually big image.

Once the Displayable Creator is launched, just drop images on the main
window to create Displayables, or drop Displayables to view them.

## Main Window

<img src="img/main.png">

The main window give access to all the functionality of the app. 

- the checkbox enable or disable sharing of Displayables over the network.
- the list icon gives access to the list of Displayables and images
  (queued for transformation into Displayables).
- the wrench icon gives access to the preferences for the app.
- the '?' icon gives access to this very document.


## Preferences

<img src="img/prefs.png">

- _Sharing port_: the network port on your machine from which the
  Displayable Creator will serve the Displayable to your
  Displayator-running iOS devices. This port need to be open in your
  firewall.
- _Activate on startup_: when ticked, the Displayable creator will
  start the network sharing functionality when launching.
- _Tile Size_: A Displayable stores a copy of the original image cut
  in tiles that can be quickly accessed. This allow you to choose the
  tile size. Bigger tiles means less tiles to show. However, bigger
  tiles might take more time to load on older device. Tooltips
  indicate suggested usage for each tile size.
- _Check for update on startup_: as it says :-).
- _Check now_: will immediately check for update. Also, if you
  previously clicked on "ignore [version]", that version will no
  longer be ignored (until ignored again).


## Displayable & Image List

<img src="img/list.png">

This list contains the images queued for transformation into
Displayable, and the Displayables ready for sharing over the local
network. Sharing is enabled/disabled on the main screen.

- The _floppy-disk icon_ marks that the Displayable is unsaved,
meaning that it will be destroyed when you quit the app. Click on it
to view the save dialog (see bellow).
- The _pencil icon_ indicate the Displayable has already been
  saved. Clicking on it will popup the save/edit dialog (see bellow).
- Clicking on the _eye icon_ will activate the Displayable viewer
  window, loading it with the selected Displayable.
- Clicking on the _x icon_ allows you to remove the Displayable or
  queued image from the list. If the Displayable is unsaved, removing
  it from the list will remove it from its temporary place on your
  hard drive.


## The Edit / Save dialog

<img src="img/save.png">

The Edit/Save dialog allows you to change the title of the
Displayable, its file name, and you can add a description.

- The _title_ is the name of the Displayable as it will appear in the
Displayator app on your iOS device.
- The _save as_ field contains the file name of the Displayable,
  without the file extension.
  your Displayable in.
- The _in directory_ field contains the directory you want to save
- The _description_ will appear on the download screen of the
  Displayable app, on top of a miniature of the image.


## Viewing Displayables

<img src="img/view.png">

When you click on the _eye_ icon in the Displayable list, the viewer
will appear with the selected Displayable. Clicking on the looking
glasses with + and - will zoom and de-zoom the Displayable.

You can move the view around by dragging the image.


## Sharing Displayables over the local network

If you have any Displayable loaded in the app, and enable the checkbox
on the main screen of the app, the Displayable will be made available
over the local network. To download Displayables Connect your iOS
device to the same network, start the Displayator app and tap on
"Download Displayables". The app should automatically find this
Displayable Creator and fetch the list of Displayable ready for
download. 

Should Displayator fail to find the list of shared Displayables, open
Safari on your iOS device and type in the address that appear at the
bottom of the Displayable Creator application. This page will contain
links to either open the list or start downloading individual
Displayables in the Displayator app.

<!--- clicking on the diagonal expanding arrows will make the view go full-screen.-->


