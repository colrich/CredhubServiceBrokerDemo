This sample shows how to write properties to be securely stored in your PAS system credhub, and read them in your apps, using the Credhub Service Broker. 

1. push this sample app
- mvn package -DskipTests
- cf push credtest -p target/TestCredhub-0.0.1-SNAPSHOT.jar -b java_buildpack_offline

If the name "credtest" is taken, substitute another one. If you do so, replace "credtest" everywhere below with the name you chose.

2. write the values into credhub
- cf create-service credhub default mycred -c '{ "password": "aoeuaoeu" }'
- cf create-service credhub default mycredmulti -c '{ "password": "aoeuaoeu", "username": "blah", "extraval": "onetuhnotuh" }'

This creates two different credential blocks, one that contains just one value and one that contains a few values. These properties are now stored in credhub and are encrypted at rest and while in motion to the container running your app.

3. bind the values to the apps that need them
- cf bind-service credtest mycred
- cf bind-service credtest mycredmulti
- cf restart credtest

Now the values you wrote into credhub in step 2 are made available to the app or apps you bind them to. Remember to restart the app after binding as the bindings take effect only on app restart.

4. verify functionality
- cf logs credtest

Wait a few seconds. Every 5 seconds, this app should print:
```
   2019-10-10T15:30:00.64-0400 [APP/PROC/WEB/0] OUT vcap: {"credhub":[{"binding_name":null,"credentials":{"password":"aoeuaoeu"},"instance_name":"mycred","label":"credhub","name":"mycred","plan":"default","provider":null,"syslog_drain_url":null,"tags":["credhub"],"volume_mounts":[]},{"binding_name":null,"credentials":{"extraval":"onetuhnotuh","password":"aoeuaoeu","username":"blah"},"instance_name":"mycredmulti","label":"credhub","name":"mycredmulti","plan":"default","provider":null,"syslog_drain_url":null,"tags":["credhub"],"volume_mounts":[]}]}
   2019-10-10T15:30:00.64-0400 [APP/PROC/WEB/0] OUT password for the mycred binding: "aoeuaoeu"
   2019-10-10T15:30:00.64-0400 [APP/PROC/WEB/0] OUT credentials map for mycredmulti: {password=aoeuaoeu, extraval=onetuhnotuh, username=blah}
```
Note that the values for the bindings are not visible via "cf env". "cf env credtest" will produce a snippet like:
```
{
 "VCAP_SERVICES": {
  "credhub": [
   {
    "binding_name": null,
    "credentials": {
     "credhub-ref": "/credhub-service-broker/credhub/fef3fe43-27b1-4bdc-b947-cf88d6f8842f/credentials"
    },
    "instance_name": "mycred",
    "label": "credhub",
    "name": "mycred",
    "plan": "default",
    "provider": null,
    "syslog_drain_url": null,
    "tags": [
     "credhub"
    ],
    "volume_mounts": []
   },
   {
    "binding_name": null,
    "credentials": {
     "credhub-ref": "/credhub-service-broker/credhub/df51f680-7d06-4649-9eb7-627820b887d8/credentials"
    },
    "instance_name": "mycredmulti",
    "label": "credhub",
    "name": "mycredmulti",
    "plan": "default",
    "provider": null,
    "syslog_drain_url": null,
    "tags": [
     "credhub"
    ],
    "volume_mounts": []
   }
  ]
 }
}
```

Only inside an app bound to the "mycred" or "mycredmulti" services can you access the secured values.
