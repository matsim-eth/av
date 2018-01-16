# CHANGELOG

- Add replanning interval parameter for SingleHeuristicDispatcher
- Bugfix: Close threads of parallel least cost path calculator
- **0.1.3**
- BC: Re-implementation of LeastCostPathCalculator component for better stability, potential fix of delayed routing issues
- AV schedules (must) have infinite end time now
- Bugfix: Exceptions due to `COMPLETED` schedules if the lifetime of vehicles was set too low
- Bugfix: IllegalStateException when requests are assigned at 30:00
- Make AV package compatible with `matsim:0.10.0-nov17`
- Bugfix: Add stuck scoring (new parameter in config), which adds a penalty if an agent is still on an AV leg when the simulation time ends, before those legs were NOT scored at all
- Bugfix: Dispatching mode fixed for MultiODHeuristic (see below)
- Bugfix: SingleHeuristicDispatcher now performs proper load balancing, before UNDERSUPPLY was only chosen if NO vehicle
available at a time, now if available vehicles < pending requests
- **0.1.2**
