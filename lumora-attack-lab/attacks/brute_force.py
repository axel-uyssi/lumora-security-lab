from models.task import AttackTask

class BruteForceAttack:

    def __init__(self, queue_manager):
        self.queue_manager = queue_manager

    def load(self, email, wordlist_path):

        with open(wordlist_path, "r", encoding="latin-1") as file:

            for line in file:

                password = line.strip()

                if not password:
                    continue

                task = AttackTask(
                    email=email,
                    password=password
                )

                self.queue_manager.add_task(task)