## UrlImageViewHelper
UrlImageViewHelper will fill an ImageView with an image that is found at a URL.

### Sample Project

The sample will do a Google Image Search and load/show the results asynchronously.


### Usage

UrlImageViewHelper will automatically download and manage all the web images and ImageViews.
Duplicate urls will not be loaded into memory twice. Bitmap memory is managed by using
a weak reference hash table, so as soon as the image is no longer used by you,
it will be garbage collected automatically.

Usage is simple:

```java
UrlImageViewHelper.setUrlDrawable(imageView,
                                  "http://example.com/image.png");
```


Want a placeholder image while it is being downloaded?

```java
UrlImageViewHelper.setUrlDrawable(imageView,
                                  "http://example.com/image.png",
                                  R.drawable.placeholder);
```


Don't want to use a placeholder resource, but a drawable instead?

```java
UrlImageViewHelper.setUrlDrawable(imageView,
                                  "http://example.com/image.png",
                                  drawable);
```


What if you want to preload images for snazzy fast loading?

```java
UrlImageViewHelper.loadUrlDrawable(context,
                                  "http://example.com/image.png");
```


What if you only want to cache the images for a minute?

```java
// Note that the 3rd argument "null" is an optional interstitial
// placeholder image.
UrlImageViewHelper.setUrlDrawable(imageView,
                                  "http://example.com/image.png",
                                  null,
                                  60000);
```

UrlImageViewHelper is pretty smart. It can even load the photo for an Android contact
if given a Contact Content Provider URI.

```java
UrlImageViewHelper.setUrlDrawable(imageView,
                                  "content://com.android.contacts/contacts/1115",
                                  R.drawable.dummy_contact_photo);
```

### FAQ

**Does it work in list adapters when views are reused? (convertView)**

Yes.
