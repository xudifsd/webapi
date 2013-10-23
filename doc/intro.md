# Introduction to webapi

this is web interface of [cloudDownload](https://github.com/xudifsd/cloudDownload), use json to exchange data between client and server. It has following two interfaces:

POST url that you want to download to `/new`, and server will return something like `{'id': 100}`, then client could use this id to retrieve the status of downloading.

`/status/id_got_from_previous_action`, server will return status of specified task id. Data returned from server varied from status of task:

* if task is finished, server will return something like

    {"id": 100, "status": "success", "retrieveUrl": "http://serverip/video"}

* if task is failed, server will return something like

    {"id": 100, "status": "failed", "reason": "download timeout"}

* if task is in progress, server will return something like

    {"id": 100, "status": "in progress", "finished": "20%"}
