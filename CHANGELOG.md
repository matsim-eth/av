# CHANGELOG

- Bugfix: Add stuck scoring (new parameter in config), which adds a penalty if an agent is still on an AV leg when the simulation time ends, before those legs were NOT scored at all
- Bugfix: Dispatching mode fixed for MultiODHeuristic (see below)
- Bugfix: SingleHeuristicDispatcher now performs proper load balancing, before UNDERSUPPLY was only chosen if NO vehicle
available at a time, now if available vehicles < pending requests
- **0.1.2**