import argparse
import os

# Parse inputs arguments
def parse_arguments():
  parser = argparse.ArgumentParser()
  parser.add_argument('--epochs', type=int, default=50, help='Number of epochs')
  parser.add_argument('--batch-size', type=int, default=256, help='Batch size')
  parser.add_argument('--dataset-size', type=int, default=20000, help='Number of elements to pick from dataset')
  parser.add_argument('--name', type=str, default="default", help='Name of test (output will be stored in /tests/name/*)')
  FLAGS, unparsed = parser.parse_known_args()
  print("NAME : {}".format(FLAGS.name))
  print("EPOCHS : {}".format(FLAGS.epochs))
  print("BATCH SIZE : {}".format(FLAGS.batch_size))
  print("DATASET SIZE : {}".format(FLAGS.dataset_size))
  return FLAGS, unparsed

# Check if directory exists and if not, create it
def ensure_dir(file_path):
  directory = os.path.dirname(file_path)
  if not os.path.exists(directory):
    os.makedirs(directory)

def ensure_dirs(test_output_dir, checkpoint_dir):
  ensure_dir('tests/dcgan/_')
  ensure_dir('{}/_'.format(test_output_dir))
  ensure_dir('{}/_'.format(checkpoint_dir))