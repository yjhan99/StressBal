import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras.regularizers import l2
from tfltransfer import bases
from tfltransfer import heads
from tfltransfer import optimizers
from tfltransfer.tflite_transfer_converter import TFLiteTransferConverter
import numpy as np
import pandas as pd

"""Define the base model.

To be compatible with TFLite Model Personalization, we need to define a
base model and a head model. 

Here we are using an identity layer for base model, which just passes the 
input as it is to the head model.
"""
base = tf.keras.Sequential(
    [tf.keras.Input(shape=(15,)), tf.keras.layers.Lambda(lambda x: x)]
)
base.compile(loss="categorical_crossentropy", optimizer="adam")
base.save("identity_model", save_format="tf")

"""Define the head model.

This is the model architecture that we will train using Flower. 
"""
head = tf.keras.Sequential(
    [
        tf.keras.Input(shape=(15,)),
        tf.keras.layers.Dense(units=2, activation="softmax"),
    ]
)
head.compile(loss="categorical_crossentropy", optimizer="adam")

train_data = pd.read_csv('traindata.csv')

X_data = train_data.loc[:, train_data.columns != 'label']
y_data = pd.DataFrame(train_data.loc[:, 'label'])
X_data = X_data.astype(np.float32)
y_data = y_data.astype(np.float32)
y_data = tf.keras.utils.to_categorical(y_data)

head.fit(
    x=X_data,
    y=y_data,
    batch_size=32,
    epochs=400
)

"""Convert the model for TFLite.

Using 10 classes in CIFAR10, learning rate = 1e-3 and batch size = 32

This will generate a directory called tflite_model with five tflite models.
Copy them in your Android code under the assets/model directory.
"""

base_path = bases.saved_model_base.SavedModelBase("identity_model")
converter = TFLiteTransferConverter(
    2, base_path, heads.KerasModelHead(head), optimizers.Adam(1e-3), train_batch_size=32
)

converter.convert_and_save("tflite_model")