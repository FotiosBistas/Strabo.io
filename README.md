# Strabo.io
A greeklish to greek translation keyboard for Android

## Authors
[Fotios Bistas](https://github.com/FotiosBistas "Fotios Bistas"), [Georgios E. Syros](https://github.com/gsiros "Georgios E. Syros"), [Anastasios Toumazatos](https://github.com/toumazatos "Anastasios Toumazatos")

## Background
Strabo.io is a custom Android software keyboard that leverages advances Machine Learning techniques to translate from Greeklish[^1] text to Greek in real time as the user types. Its core functionality revolves around the translation model. Training data is collected and sent anonymously and encrypted to a central server where model retraining occurs. If a model is produced with better loss than the current, it gets distributed upon client request to the end nodes.

<p align="center">
  <img src="https://github.com/FotiosBistas/Strabo.io/assets/47118034/276602ed-6302-4f54-b47c-b0819d75e390" width="300" height="220">
</p>

<p align="center">
  <i><b>Figure 1</b>. Keyboard nodes send training data to the server.</i>
</p>

<p align="center">
  <img src="https://github.com/FotiosBistas/Strabo.io/assets/47118034/dc614221-c263-4033-aae8-649ed5c99365" width="300" height="220">
</p>

<p align="center">
  <i><b>Figure 2</b>. The server distributes the new model.</i>
</p>

<p align="center">
<img src="https://user-images.githubusercontent.com/47118034/231530358-0c451796-5b1e-4252-8105-d669da5a69c3.gif" alt="Brief demo of the prototype" width="300" height="650">
</p>

<p align="center">
  <i><b>Screenshot 1</b>. Keyboard translation demo.</i>
</p>

<p align="center">
<img src="https://github.com/FotiosBistas/Strabo.io/assets/47118034/1b7a765c-baed-4580-b650-46969d74eeba"  width="300" height="650">
</p>

<p align="center">
  <i><b>Screenshot 2</b>. Keyboard settings.</i>
</p>

<p align="center">
<img src="https://github.com/FotiosBistas/Strabo.io/assets/47118034/b046da2f-c3e3-47ed-a419-bd1085150c8c"  width="300" height="650">
</p>

<p align="center">
  <i><b>Screenshot 3</b>. New model popup.</i>
</p>

<p align="center">
<img src="https://github.com/FotiosBistas/Strabo.io/assets/47118034/c57a341f-ce0c-4589-bedc-8b264fb222c1"  width="300" height="650">
</p>

<p align="center">
  <i><b>Screenshot 4</b>. Model downloading.</i>
</p>

[^1]: https://en.wikipedia.org/wiki/Greeklish
