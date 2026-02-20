PAPER_JAR  := dd-paper/target/dd-paper-1.0.0.jar
VELOCITY_JAR := dd-velocity/target/dd-velocity-1.0.0.jar

PAPER_SERVERS := overworld nether end

.PHONY: build deploy all release start stop logs

all: build deploy

build:
	mvn clean package -q

deploy: $(PAPER_JAR) $(VELOCITY_JAR)
	$(foreach server,$(PAPER_SERVERS),cp $(PAPER_JAR) .dev/paper/$(server)/plugins/;)
	cp $(VELOCITY_JAR) .dev/velocity/plugins/

$(PAPER_JAR) $(VELOCITY_JAR):
	$(MAKE) build

release:
	mvn clean package -q
	@mkdir -p release
	cp $(PAPER_JAR) release/
	cp $(VELOCITY_JAR) release/
	@echo "JARs disponibles dans release/ :"
	@ls -1 release/


	docker compose up -d

stop:
	docker compose stop

logs:
	docker compose logs -f
