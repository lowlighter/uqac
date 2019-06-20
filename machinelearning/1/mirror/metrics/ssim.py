
import tensorflow as tf
import os 
import pathlib

max_v = [0, 0, 0, 0]
max_p = ["", "", "", ""]
moy_v = 0

# Read images from file.
im1 = tf.image.decode_png(tf.io.read_file(os.path.join(os.getcwd(), "models/style-gan/results/uncared-anime", "uncurated-anime-0.png")))
images = [str(path) for path in list(pathlib.Path(os.path.join(os.getcwd(), "datasets/datanime")).glob('*'))]

for imp in images:
  im2 = tf.image.decode_png(tf.io.read_file(imp))
  ssim = tf.image.ssim(im1, im2, max_val=255)
  moy_v += ssim

  for i, v in enumerate(max_v):
    if (ssim > v):
      max_v[i] = ssim
      max_p[i] = imp
      break

print(max_v, max_p)
print(moy_v/len(images))
