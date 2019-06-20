import tensorflow as tf
import pathlib
import matplotlib.pyplot as plt
import numpy as np
from tensorflow.keras import backend as K

# Preprocess image by applying transformations on loaded image
#TODO: Blacklist
def preprocess_image(path):
	image = tf.io.read_file(path)
	image = tf.image.decode_png(image, channels=3)
	image = tf.image.resize(image, [64, 64])
	image /= 255.0
	return image
	
def make_preprocessor(DATASET_SIZE, BATCH_SIZE):

	# Recuperation des fichiers images
	images = [str(path) for path in list(pathlib.Path("datasets/datanime").glob('*'))]

	# Creation d'un tf.Dataset a partir des fichiers images
	paths_ds = tf.data.Dataset.from_tensor_slices(images)

	# Appel de la fonction de preprocessing sur le dataset
	images_ds = paths_ds.map(preprocess_image, num_parallel_calls=tf.data.experimental.AUTOTUNE).shuffle(DATASET_SIZE).batch(BATCH_SIZE)

	return images_ds


