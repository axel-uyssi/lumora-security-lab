import logging

logging.basicConfig(
    filename="attack.log",
    level=logging.INFO,
    format="%(asctime)s | %(message)s"
)

logger = logging.getLogger("simulator")