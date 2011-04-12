UrlImageViewHelper will fill an ImageView with an image that is found at a URL.

The sample will do a Google Image Search and load/show the results asynchronously.

UrlImageViewHelper will automatically download, save, and cache all the image urls
the BitmapDrawables. Duplicate urls will not be loaded into memory twice.
Bitmap memory is managed by using a weak reference hash table, so as soon as the
image is no longer used by you, it will be garbage collected automatically.

Usage is simple:

UrlImageViewHelper.setUrlDrawable(imageView, "http://example.com/image.png");

Want a placeholder image while it is being downloaded?

UrlImageViewHelper.setUrlDrawable(imageView, "http://example.com/image.png", R.drawable.placeholder);

Don't want to use a placeholder resource, but a drawable instead?

UrlImageViewHelper.setUrlDrawable(imageView, "http://example.com/image.png", drawable);

What if you want to preload images for snazzy fast loading?

UrlImageViewHelper.loadUrlDrawable(context, "http://example.com/image.png");

What if you only want to cache the images for a minute?

UrlImageViewHelper.setUrlDrawable(imageView, "http://example.com/image.png", /* This an optional interstitial argument */ null, 60000);


TODO: An http connection manager for connection reuse might be handy.