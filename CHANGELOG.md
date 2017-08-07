# CHANGELOG

- Bugfix: SingleHeuristicDispatcher now performs proper load balancing, before UNDERSUPPLY was only chosen if NO vehicle
available at a time, now if available vehicles < pending requests
- **0.1.2**