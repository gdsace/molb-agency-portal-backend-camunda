INSERT INTO agency (code, name) VALUES ('pub', 'Public Utilities Board');

INSERT INTO licence_type (licence_id, name, agency_id)
VALUES ('268', 'Plumber Licence', (SELECT id FROM agency WHERE code = 'pub')),
       ('4', 'Written Approval for the Discharge of Trade Effluent Into the Public Sewerage', (SELECT id FROM agency WHERE code = 'pub'));



