import tensorflow as tf
import pathlib

# Preprocess image by applying transformations on loaded image
#TODO: Blacklist
def preprocess_image(path):
  image = tf.io.read_file(path)
  image = tf.image.decode_png(image, channels=3)
  image = tf.image.resize(image, [64, 64])
  print(image)
  image = tf.image.rgb_to_grayscale(image)
  print(tf.image.grayscale_to_rgb(image))
  image /= 255.0
  return image


def make_preprocessor(DATASET_SIZE, BATCH_SIZE):

	# Récupération des fichiers images
	images = [str(path) for path in list(pathlib.Path("datasets/datanime").glob('*'))]

	# Création d'un tf.Dataset à partir des fichiers images
	paths_ds = tf.data.Dataset.from_tensor_slices(images)

	# Appel de la fonction de préprocessing sur le dataset
	images_ds = paths_ds.map(preprocess_image, num_parallel_calls=tf.data.experimental.AUTOTUNE).shuffle(DATASET_SIZE).batch(BATCH_SIZE)

	return images_ds


