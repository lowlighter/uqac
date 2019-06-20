# Importing the Keras libraries and packages
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Dense
import pathlib
import os
import re


# Preprocess image by applying transformations on loaded image
#TODO: Blacklist
def preprocess_image(path):
  image = tf.io.read_file(path)
  image = tf.image.decode_png(image, channels=3)
  image = tf.image.resize(image, [64, 64])
  image = tf.cast(image, tf.float32)
  image /= 255.0
  return image


paths = list(pathlib.Path(os.path.join(os.getcwd(), "datasets/datanime")).glob('*'))

images_ds = tf.data.Dataset.from_tensor_slices([str(path) for path in paths])
images_ds = images_ds.map(preprocess_image, num_parallel_calls=tf.data.experimental.AUTOTUNE)

labels_ds = tf.data.Dataset.from_tensor_slices(tf.cast([int(re.search("([0-9]+).png$", str(path)).group(1)) for path in paths], tf.int32))

images_labels_ds = tf.data.Dataset.zip((images_ds, labels_ds))
images_labels_ds = images_labels_ds.batch(512)

model = tf.keras.models.Sequential([
  tf.keras.layers.Conv2D(32, (3, 3), input_shape=(64, 64, 3), activation="relu"),
  tf.keras.layers.MaxPooling2D(pool_size=(2, 2)),
  tf.keras.layers.Conv2D(32, (3, 3), input_shape=(32, 32, 3), activation="relu"),
  tf.keras.layers.MaxPooling2D(pool_size=(2, 2)),
  tf.keras.layers.Flatten(),
  tf.keras.layers.Dense(128, activation="relu"),
  tf.keras.layers.Dense(1, activation="sigmoid")
])
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

model.fit(images_labels_ds, epochs=50)
model.save('ssim-model.h5')




#paths = [str(path) for path in list(pathlib.Path(os.path.join(os.getcwd(), "models/style-gan/results/uncared-anime"))).glob('*'))]
#X = [numpy.array(Image.open(os.path.join(os.getcwd(), "models/style-gan/results/uncared-anime", "uncurated-anime-0.png"))).flatten()]

#print(numpy.array([c.predict(X) for c in clf.estimators_]).flatten())

#print(clf.predict(X))
