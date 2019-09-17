# CHANGELOG

- Update to MATSim 12 weekly SNAPSHOOT 2019w37
- Copy analysis output to main output folder if present at simulation end
- Fix: Distances should be measured through Link*Enter*Events

**1.0.0**

- Fix bug when using fallback value for dynamic waiting time
- Fix fleet size bug after MATSim 12 upgrade
- Added analysis tools
- Include price calculation in routing
- Optionally preroute any AV trip to predict travel times
- BC: Make vehicle type configurable per operator
- BC: Add waiting time estimation functionality
- BC: Add filters for operating areas and use distinct network for each operator
- BC: Streamline config (no separate config file anymore, everything included in MATSim config)

**0.3.9**

- Update repository structure to GitFlow
- Compatibility with MATSim 12
- Fix & test pickup/dropoff duration by stop and passenger
- Add access/egress walk functionality (off / by network mode / by link attribute)
- Add earliestDepartureTime to pickup activities

**0.3.8**

- Compatibility with MATSim 11.0

**0.3.7**

- Fix submission time for AVRequest

**0.1.6**

- Add router customization

**0.1.5**

- Send aggregation events in MultiOD at correct time
- Make number of seats configurable (and use in MultiODHeuristic)
- Default reoptimization interval for MultiODHeuristic and SingleHeuristicDispatcher set to 10s
- Bugifx MultiODHeuristic: Reoptimization was only triggered if vehicle/request was added in previous period
- Throw exception when a request with unknown links is created
- Bugfix PopulationDensityGenerator: Use proper random number generator
- Bugfix SingleHeuristicDispatcher: Reoptimization was only triggered if vehicle/request was added in previous period

**0.1.4**

- Added test jars to deployment
- BC: Switch from the custom QSim to a proper AbstractQSimPlugin. Make sure to register the plugin when using the AV package!

**0.1.3**

- Add replanning interval parameter for SingleHeuristicDispatcher
- Bugfix: Close threads of parallel least cost path calculator

**0.1.2**

- BC: Re-implementation of LeastCostPathCalculator component for better stability, potential fix of delayed routing issues
- AV schedules (must) have infinite end time now
- Bugfix: Exceptions due to `COMPLETED` schedules if the lifetime of vehicles was set too low
- Bugfix: IllegalStateException when requests are assigned at 30:00
- Make AV package compatible with `matsim:0.10.0-nov17`
- Bugfix: Add stuck scoring (new parameter in config), which adds a penalty if an agent is still on an AV leg when the simulation time ends, before those legs were NOT scored at all
- Bugfix: Dispatching mode fixed for MultiODHeuristic (see below)
- Bugfix: SingleHeuristicDispatcher now performs proper load balancing, before UNDERSUPPLY was only chosen if NO vehicle
available at a time, now if available vehicles < pending requests
